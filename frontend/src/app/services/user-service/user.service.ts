import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EMPTY, expand, map, Observable, reduce } from 'rxjs';
import { userApiUrl } from '../../../environements/environement';

export interface UserDto {
  uid: string;
  cn: string;
  sn: string;
  nom?: string;
  mail: string;
  o: string;
  telephoneNumber: string;
  givenName: string;
  prenom?: string;
  firstName?: string;
  lastName?: string;
}

export interface PagedResult<T> {
  items: T[];
  nextPageCookie: string | null;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly apiUrl = userApiUrl;

  constructor(private http: HttpClient) { }


  listUsersPaged(pageSize: number, cookie?: string, search?: string): Observable<PagedResult<UserDto>> {
    let url = `${this.apiUrl}?pageSize=${pageSize}`;
    if (cookie) url += `&cookie=${encodeURIComponent(cookie)}`;
    if (search?.trim()) url += `&search=${encodeURIComponent(search.trim())}`;
    return this.http.get<PagedResult<UserDto>>(url);
  }


  listUsers(): Observable<UserDto[]> {
    const pageSize = 1000;

    return this.http.get<PagedResult<UserDto>>(`${this.apiUrl}?pageSize=${pageSize}`).pipe(
      expand(response =>
        response.nextPageCookie
          ? this.http.get<PagedResult<UserDto>>(
              `${this.apiUrl}?pageSize=${pageSize}&cookie=${encodeURIComponent(response.nextPageCookie)}`
            )
          : EMPTY
      ),
      map(response => response.items),
      reduce((allUsers, pageUsers) => [...allUsers, ...pageUsers], [] as UserDto[])
    );
  }


  updateUser(uid: string, data: { uid: string; sn: string; givenName: string; mail: string; o: string; telephoneNumber: string; }): Observable<any> {
    return this.http.put(`${this.apiUrl}/${uid}`, data);
  }


  resetPassword(uid: string, newPassword: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${uid}/password`, { newPassword });
  }


  changeOwnPassword(currentPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/me/password`, { currentPassword, newPassword });
  }


  createUser(data: { uid: string; sn: string; givenName: string; mail: string; password: string; o: string; telephoneNumber: string; }): Observable<any> {
    return this.http.post(this.apiUrl, data);
  }


  deleteUser(uid: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${uid}`);
  }
}