import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { groupApiUrl } from '../../../environements/environement';

export interface GroupDto {
  cn: string;
  o: string;
  members: string[];
}

@Injectable({ providedIn: 'root' })
export class GroupService {
  private readonly apiUrl = groupApiUrl;

  constructor(private http: HttpClient) {}

  listGroups(): Observable<GroupDto[]> {
    return this.http.get<GroupDto[]>(this.apiUrl);
  }

  createGroup(cn: string, initialMemberUid: string,isAdmin:boolean,isGestionnaireUtilisateur:boolean,isGestionnaireOrganisation:boolean): Observable<any> {
    return this.http.post(this.apiUrl, { cn, initialMemberUid ,isAdmin,isGestionnaireUtilisateur,isGestionnaireOrganisation});
  }

  deleteGroup(cn: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${cn}`);
  }

  addMember(cn: string, uid: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${cn}/members`, { uid });
  }

  removeMember(cn: string, uid: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${cn}/members/${uid}`);
  }
}