import { Routes } from '@angular/router';
import { LoginPage } from './features/login-page/login-page';
import { DashboardPage } from './features/dashboard-page/dashboard-page';
import { AdminUsersPage } from './features/admin-users-page/admin-users-page';
import { ChangePasswordPage } from './features/change-password-page/change-password-page';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { AdminGroupsPage } from './features/admin-groups-page/admin-groups-page';
import { AdminStructuresPage } from './features/admin-structures-page/admin-structures-page';
import { gestionnaireUtilisateursGuard } from './core/guards/gestionnaireUtilisateurs.guard';
import { adminOrGestionnaireUtilisateursGuard } from './core/guards/adminOrGestionnaireUtilisateurs.guard';
import { adminOrGestionnaireOrganisationGuard } from './core/guards/adminOrGestionnaireOrganisation.guard';
import { DefaultLayout } from './layouts/default-layout/default-layout';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: 'menu', component: DefaultLayout, canActivate: [authGuard],
    children:[
      {path:'dashboard',component:DashboardPage,canActivate:[authGuard]},
      {path: 'admin/users', component: AdminUsersPage, canActivate: [authGuard,adminOrGestionnaireUtilisateursGuard]},
      {path: 'admin/groups', component: AdminGroupsPage, canActivate: [authGuard,adminGuard] },
      {path: 'admin/organisations', component:AdminStructuresPage,canActivate:[authGuard,adminOrGestionnaireOrganisationGuard]}
  ]
  },
  { path: 'change-password', component: ChangePasswordPage, canActivate: [authGuard] },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];