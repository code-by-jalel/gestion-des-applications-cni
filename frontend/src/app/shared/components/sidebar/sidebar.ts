import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth-service';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar implements OnInit {
  expandedMenus: Set<string> = new Set(['services', 'jobs']);
  private router = inject(Router);
  roles: String[] = []
  constructor(private authService: AuthService) { }
  ngOnInit(): void {
    this.roles = this.authService.getRoles();
    console.log(this.roles);
  }
  onclick() {
    console.log("CLICK WORKS");
  }

  toggleMenu(menu: string) {
    if (this.expandedMenus.has(menu)) {
      this.expandedMenus.delete(menu);
    } else {
      this.expandedMenus.add(menu);
    }
  }

  isExpanded(menu: string): boolean {
    return this.expandedMenus.has(menu);
  }

  isRouteActive(route: string): boolean {
    return this.router.url.startsWith(route);
  }
  logout(){
    localStorage.removeItem('auth_token');
    this.router.navigate(['/login'])
  }
}
