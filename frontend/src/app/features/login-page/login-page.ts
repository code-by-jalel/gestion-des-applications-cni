import { ChangeDetectorRef, Component } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { DefaultLayout } from "../../layouts/default-layout/default-layout";

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage {

  userForm = new FormGroup({
    email: new FormControl('', { nonNullable: true }),
    password: new FormControl('', { nonNullable: true })
  });

  errorMessage = '';
  isJalel=false
  jalel = {
    email:"jaleleddine.benromdhane@gmail.com",
    password:"1234"
  };
  constructor(private authService: AuthService, private router: Router,private cdr:ChangeDetectorRef) {}
  jalelButton(){
    this.isJalel=true;
  }
  submit() {
    // if (this.userForm.invalid) return;

    let { email, password } = this.userForm.value;
    if(this.isJalel){
      email = this.jalel.email;
      password=this.jalel.password;
    }
    this.authService.login(email!, password!).subscribe({
      next: () => {
        this.router.navigate(['/menu'])
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = 'Adresse e-mail ou mot de passe invalide'
        this.cdr.markForCheck();
      }
    });
  }
}