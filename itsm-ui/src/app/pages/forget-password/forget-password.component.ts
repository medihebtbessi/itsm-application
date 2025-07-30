import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { FormsModule, NgModel } from '@angular/forms';
import { Route, Router } from '@angular/router';

@Component({
  selector: 'app-forget-password',
  imports: [FormsModule],
  templateUrl: './forget-password.component.html',
  styleUrl: './forget-password.component.scss'
})
export class ForgetPasswordComponent {

  constructor(private authService:AuthService, private route:Router) { }
  email: string = '';
  errorMsg: string[] = [];
  isLoading = false;
  forgetPassword() {
    this.errorMsg = [];
    this.isLoading = true;

    this.authService.modifyPassword({email:this.email}).subscribe({
      next: () => {
        this.isLoading = false;
        this.route.navigate(['/modifying-password']);
        
      },
      error: (err) => {
        this.isLoading = false;
        console.log(err);
        if (err.error.validationErrors) {
          this.errorMsg = err.error.validationErrors;
        } else {
          this.errorMsg.push(err.error.errorMsg);
        }
      }
    });
  }

}
