import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "../../services/auth-service";
import { inject } from "@angular/core";

export const adminOrGestionnaireOrganisationGuard:CanActivateFn=()=>{
    const authService = inject(AuthService);
    const router = inject(Router);
    
    if (authService.isAuthenticated() && (authService.isGestionnaireOrganisation() ||authService.isAdmin())) {
    return true;
  }
    
    router.navigate(['/dashboard']);
    return false
}