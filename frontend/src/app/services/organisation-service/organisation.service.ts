import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { organisationApiUrl } from '../../../environements/environement';
export interface organisationDto {
  ou: string;
  description: string;
}
export interface OrgNode {
  ou: string;
  description: string | null;
  dn: string;
  children: OrgNode[];
}
@Injectable({
  providedIn: 'root',
})
export class OrganisationService {

  private readonly apiUrl = organisationApiUrl;
  constructor(private http: HttpClient) { }

  listOrganisations(): Observable<organisationDto[]> {

    return this.http.get<Record<string, string | null>>(this.apiUrl)
      .pipe(
        map(data =>
          Object.entries(data).map(([ou, description]) => ({
            ou,
            description: description ?? ''
          }))
        )
      );
  }
  getTree(search = ''): Observable<OrgNode[]> {
    const params = search.trim() ? `?search=${encodeURIComponent(search)}` : '';
    return this.http.get<OrgNode[]>(`${this.apiUrl}/tree${params}`);
  }
  create(ou: string, description: string, parentDn: string): Observable<any> {
    return this.http.post(this.apiUrl, { ou, description, parentDn });
  }
  update(dn: string, description: string): Observable<any> {
    return this.http.put(`${this.apiUrl}?dn=${encodeURIComponent(dn)}`, { description });
  }
  delete(dn: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}?dn=${encodeURIComponent(dn)}`);
  }
}
