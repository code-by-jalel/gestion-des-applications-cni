import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { dashboardApiUrl } from '../../../environements/environement';

export interface OrgUserCount { org: string; count: number; }
export interface GroupMemberCount { groupName: string; memberCount: number; }

export interface DashboardStats {
  totalUsers: number;
  totalGroups: number;
  totalOrganisations: number;
  usersByOrg: OrgUserCount[];
  topGroups: GroupMemberCount[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly apiUrl = dashboardApiUrl;

  constructor(private http: HttpClient) {}

  getStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
  }
}