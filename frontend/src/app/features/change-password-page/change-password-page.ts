import { Component } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../services/user-service/user.service';

@Component({
  selector: 'app-change-password-page',
  imports: [ReactiveFormsModule],
  templateUrl: './change-password-page.html',
  styleUrl: './change-password-page.scss'
})
export class ChangePasswordPage {
  form = new FormGroup({
    currentPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    newPassword: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(6)] })
  });

  message = '';

  constructor(private userService: UserService) {}

  submit() {
    if (this.form.invalid){
      console.log("form invalid");
      return;
    } 

    const { currentPassword, newPassword } = this.form.value;

    this.userService.changeOwnPassword(currentPassword!, newPassword!).subscribe({
      next: () => {
        this.message = 'Password updated successfully';
        this.form.reset();
      },
      error: () => this.message = 'Failed to update password — check your current password'
    });
  }
}