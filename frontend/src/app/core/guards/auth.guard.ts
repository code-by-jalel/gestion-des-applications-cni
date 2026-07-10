import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // console.log('[authGuard] token:', authService.getToken());
  // console.log('[authGuard] isAuthenticated:', authService.isAuthenticated());

  if (authService.isAuthenticated()) {
    return true;
  }

  // console.log('[authGuard] redirecting to login');
  router.navigate(['/login']);
  return false;
};