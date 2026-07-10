import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "../../services/auth-service";
import { inject } from "@angular/core";

export const gestionnaireUtilisateursGuard:CanActivateFn=()=>{
    const authService = inject(AuthService);
    const router = inject(Router);
    
    if (authService.isAuthenticated() && authService.isgestionnaireUtilisateurs()) {
        console.log("[gestionnaire_utilisateur]: true");
    return true;
  }
    
    router.navigate(['/dashboard']);
    return false
}