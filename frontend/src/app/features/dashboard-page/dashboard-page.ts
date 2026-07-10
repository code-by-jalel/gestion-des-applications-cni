import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService, DashboardStats } from '../../services/dashboard-service/dashboard.service';

@Component({
  selector: 'app-dashboard-page',
  imports: [CommonModule],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss'
})
export class DashboardPage implements OnInit {
  stats: DashboardStats | null = null;
  loading = true;
  error = '';

  constructor(
    private dashboardService: DashboardService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.dashboardService.getStats().subscribe({
    
      next: stats => {
        console.log(stats);
        this.stats = stats;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: err => {
        this.error = `Erreur de chargement: ${err.status}`;
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  barWidth(value: number, max: number): number {
    if (max === 0) return 0;
    return Math.round((value / max) * 100);
  }

  get maxOrgCount(): number {
    return Math.max(...(this.stats?.usersByOrg.map(o => o.count) ?? [1]));
  }

  get maxGroupCount(): number {
    return Math.max(...(this.stats?.topGroups.map(g => g.memberCount) ?? [1]));
  }

}