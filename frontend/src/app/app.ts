import { Component, computed, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

type Permission = 'USER_ACCESS' | 'ADMINISTRATOR_USER_ACCESS' | 'NO_ACCESS';
type Theme = 'light' | 'gray' | 'black';
type Page = 'library' | 'reader' | 'admin';

interface UserResponse {
  userID: string;
  permissions: Permission[];
}

interface LoginResponse extends UserResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
}

interface KnowledgeBase {
  id: string;
  title: string;
  version: string;
  description: string | null;
  contentType: string;
  folderNameVersion: string;
  author: string | null;
  emailAddress: string | null;
  tableOfContents: string | null;
}

interface KnowledgeBaseForm {
  title: string;
  folderNameVersion: string;
  version: string;
  contentType: string;
  description: string;
  author: string;
  emailAddress: string;
  tableOfContents: string;
}

const EMPTY_KNOWLEDGE_BASE: KnowledgeBaseForm = {
  title: '', folderNameVersion: '', version: '', contentType: 'Free',
  description: '', author: '', emailAddress: '', tableOfContents: '',
};

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  host: { '[attr.data-theme]': 'theme()' },
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly user = signal<UserResponse | null>(null);
  protected readonly knowledgeBases = signal<KnowledgeBase[]>([]);
  protected readonly selectedKnowledgeBase = signal<KnowledgeBase | null>(null);
  protected readonly page = signal<Page>('library');
  protected readonly theme = signal<Theme>(this.readTheme());
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');
  protected readonly search = signal('');
  protected readonly hasReaderAccess = computed(() => this.user()?.permissions.some(
    permission => permission === 'USER_ACCESS' || permission === 'ADMINISTRATOR_USER_ACCESS',
  ) ?? false);
  protected readonly isAdministrator = computed(() => this.user()?.permissions.includes('ADMINISTRATOR_USER_ACCESS') ?? false);
  protected readonly filteredKnowledgeBases = computed(() => {
    const query = this.search().trim().toLocaleLowerCase();
    if (!query) return this.knowledgeBases();
    return this.knowledgeBases().filter(item =>
      [item.title, item.version, item.description, item.contentType]
        .some(value => value?.toLocaleLowerCase().includes(query)),
    );
  });

  protected userID = '';
  protected password = '';
  protected knowledgeBaseForm: KnowledgeBaseForm = { ...EMPTY_KNOWLEDGE_BASE };
  protected targetUserID = '';
  protected permissionToChange: Permission = 'USER_ACCESS';
  private authorization = '';

  constructor(private readonly http: HttpClient) {}

  protected async signIn(): Promise<void> {
    this.clearMessages();
    const userID = this.userID.trim();
    const password = this.password;
    if (!userID || !password) {
      this.errorMessage.set('Enter your user ID and password.');
      return;
    }
    this.loading.set(true);
    try {
      const result = await firstValueFrom(this.http.post<LoginResponse>('/api/users/login', { userID, password }));
      if (!result) throw new Error('The server returned an empty login response.');
      this.authorization = `${result.tokenType} ${result.accessToken}`;
      this.user.set(result);
      this.password = '';
      if (this.hasReaderAccess()) {
        this.page.set('library');
        await this.loadKnowledgeBases();
        if (!this.errorMessage()) this.successMessage.set(`Welcome back, ${result.userID}.`);
      } else {
        this.successMessage.set('Your account is valid, but it has no application access yet. Ask an administrator to grant access.');
      }
    } catch (error) {
      this.errorMessage.set(this.describeError(error, 'Sign-in failed. Check your credentials and try again.'));
    } finally {
      this.loading.set(false);
    }
  }

  protected async signOut(): Promise<void> {
    let logoutError = '';
    try {
      await firstValueFrom(this.http.post<void>('/api/users/logout', {}));
    } catch (error) {
      logoutError = this.describeError(error, 'The server could not clear the login cookie.');
    }
    this.authorization = '';
    this.user.set(null);
    this.knowledgeBases.set([]);
    this.selectedKnowledgeBase.set(null);
    this.userID = '';
    this.password = '';
    this.page.set('library');
    this.clearMessages();
    if (logoutError) this.errorMessage.set(logoutError);
  }

  protected async loadKnowledgeBases(): Promise<void> {
    this.clearMessages();
    this.loading.set(true);
    try {
      const list = await firstValueFrom(this.http.get<KnowledgeBase[]>('/api/knowledge-bases', { headers: this.authHeaders() }));
      this.knowledgeBases.set(list ?? []);
    } catch (error) {
      this.errorMessage.set(this.describeError(error, 'Could not load Knowledge Bases.'));
    } finally {
      this.loading.set(false);
    }
  }

  protected openKnowledgeBase(item: KnowledgeBase): void {
    this.selectedKnowledgeBase.set(item);
    this.page.set('reader');
    this.clearMessages();
  }

  protected async registerKnowledgeBase(): Promise<void> {
    if (!this.isAdministrator()) return;
    this.clearMessages();
    this.loading.set(true);
    try {
      const created = await firstValueFrom(this.http.post<KnowledgeBase>('/api/knowledge-bases', this.knowledgeBaseForm, {
        headers: this.authHeaders(),
      }));
      if (created) {
        this.knowledgeBases.update(items => [created, ...items]);
        this.knowledgeBaseForm = { ...EMPTY_KNOWLEDGE_BASE };
        this.successMessage.set(`${created.title} was registered successfully.`);
      }
    } catch (error) {
      this.errorMessage.set(this.describeError(error, 'Could not register this Knowledge Base.'));
    } finally {
      this.loading.set(false);
    }
  }

  protected async changePermission(action: 'grant' | 'revoke'): Promise<void> {
    if (!this.isAdministrator()) return;
    const userID = this.targetUserID.trim();
    if (!userID) {
      this.errorMessage.set('Enter the user ID to update.');
      return;
    }
    this.clearMessages();
    this.loading.set(true);
    const method = action === 'grant' ? 'put' : 'delete';
    try {
      await firstValueFrom(this.http.request<UserResponse>(method, `/api/users/${encodeURIComponent(userID)}/permissions`, {
        headers: this.authHeaders(),
        body: { permission: this.permissionToChange },
      }));
      this.successMessage.set(`${this.permissionToChange.replaceAll('_', ' ')} ${action === 'grant' ? 'granted to' : 'revoked from'} ${userID}.`);
      this.targetUserID = '';
    } catch (error) {
      this.errorMessage.set(this.describeError(error, `Could not ${action} permission.`));
    } finally {
      this.loading.set(false);
    }
  }

  protected setTheme(value: string): void {
    if (value !== 'light' && value !== 'gray' && value !== 'black') return;
    this.theme.set(value);
    localStorage.setItem('togafmate-theme', value);
  }

  protected setThemeFromEvent(event: Event): void {
    this.setTheme((event.target as HTMLSelectElement).value);
  }

  protected updateSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  protected navigate(page: Page): void {
    if (page === 'admin' && !this.isAdministrator()) return;
    if (page !== 'admin' && !this.hasReaderAccess()) return;
    this.page.set(page);
    this.selectedKnowledgeBase.set(null);
    this.clearMessages();
    if (page === 'library') void this.loadKnowledgeBases();
  }

  private authHeaders(): HttpHeaders {
    return new HttpHeaders({ Authorization: this.authorization });
  }

  private describeError(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { detail?: string; message?: string; title?: string } | null;
      if (error.status === 0) return 'The API is unavailable. Check that the backend is running and try again.';
      return body?.detail ?? body?.message ?? body?.title ?? (error.status === 401 ? 'Your credentials were not accepted.' : fallback);
    }
    return error instanceof Error ? error.message : fallback;
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  private readTheme(): Theme {
    const saved = typeof localStorage === 'undefined' ? null : localStorage.getItem('togafmate-theme');
    return saved === 'gray' || saved === 'black' ? saved : 'light';
  }
}
