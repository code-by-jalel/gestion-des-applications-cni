import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

interface LoginResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8081/api/auth';
  private readonly tokenKey = 'auth_token';

  constructor(private http: HttpClient) { }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, { username:email, password })
      .pipe(
        tap(res => localStorage.setItem(this.tokenKey, res.token))
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  getToken(): string | null {
    if (typeof window === 'undefined') {
      return null;
    }
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private decodeToken(token: string): any {
    const payload = token.split('.')[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  }

  getRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];

    try {
      const payload = this.decodeToken(token);
      return payload.roles ? payload.roles.split(',') : [];
    } catch {
      return [];
    }
  }
  isgestionnaireUtilisateurs(){
    console.log("verifying ROLE_GESTIONNAIREUTILISATEURS");
    return this.getRoles().includes('ROLE_GESTIONNAIREUTILISATEURS');
  }
  isGestionnaireOrganisation(){
    return this.getRoles().includes("ROLE_GESTIONNAIREORGANISATION");
  }
  isAdmin(): boolean {
    return this.getRoles().includes('ROLE_ADMINSGROUP');
  }
//   getStructure(): string | null {
//   const token = this.getToken();
//   if (!token) return null;
//   try {
//     return this.decodeToken(token).structure ?? null;
//   } catch {
//     return null;
//   }
// }
}