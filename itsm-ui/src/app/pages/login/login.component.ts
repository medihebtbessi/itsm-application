// login.component.ts
import { Component } from '@angular/core';
import { AuthenticationRequest } from '../../models/AuthenticationRequest';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TokenService } from '../../services/token.service';
import { NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User, UserService } from '../user/user.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [ FormsModule, NgIf],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  authRequest: AuthenticationRequest = { email: '', password: '' };
  errorMsg: Array<string> = [];
  isLoading = false;
  
  emailTouched = false;
  passwordTouched = false;
  connectedUser: User | null = null;

  constructor(
    private router: Router,
    private authService: AuthService,
    private tokenService: TokenService,
    private route: ActivatedRoute,
    private userService: UserService,
    private toastr: ToastrService
  ) {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (token) {
        localStorage.setItem('jwt', token);
        this.handleOAuthSuccess();
      }
    });
  }

  
  login(): void {
    this.resetErrors();
    this.isLoading = true;
    this.markFieldsAsTouched();

    if (!this.isFormValid()) {
      this.isLoading = false;
      return;
    }

    this.authService.authenticate(this.authRequest).subscribe({
      next: (res) => {
        this.toastr.success("Logged succesfully","Success");
        this.handleLoginSuccess(res.token);
      },
      error: (err) => {
        this.toastr.error("Login failed","Error");
        this.handleLoginError(err);
      }
    });
  }

  
  register(): void {
    this.router.navigate(['register']);
  }

 
  forgotPassword(): void {
    this.router.navigate(['/forget-password']);
  }

  
  loginWithGitHub(): void {
    window.location.href = 'http://localhost:8090/oauth2/authorization/github';
  }

 
  loginWithGoogle(): void {
    window.location.href = 'http://localhost:8090/oauth2/authorization/google';
  }

 
  isFormValid(): boolean {
    return this.isEmailValid() && this.isPasswordValid();
  }

 
  isEmailValid(): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return !!(this.authRequest.email && emailRegex.test(this.authRequest.email));
  }

  
  isPasswordValid(): boolean {
    return !!(this.authRequest.password && this.authRequest.password.trim().length > 0);
  }

  
  getEmailError(): string {
    if (!this.emailTouched) return '';
    if (!this.authRequest.email || this.authRequest.email.trim() === '') {
      return 'L\'adresse email est requise';
    }
    if (!this.isEmailValid()) {
      return 'Format d\'email invalide';
    }
    return '';
  }

  
  getPasswordError(): string {
    if (!this.passwordTouched) return '';
    if (!this.authRequest.password || this.authRequest.password.trim() === '') {
      return 'Le mot de passe est requis';
    }
    return '';
  }

  
  onEmailBlur(): void {
    this.emailTouched = true;
  }

  
  onPasswordBlur(): void {
    this.passwordTouched = true;
  }

  
  clearErrors(): void {
    this.errorMsg = [];
  }

  
  private resetErrors(): void {
    this.errorMsg = [];
  }

  
  private markFieldsAsTouched(): void {
    this.emailTouched = true;
    this.passwordTouched = true;
  }

  
  private handleLoginSuccess(token: string): void {
    this.isLoading = false;
    this.tokenService.token = token;
    
    this.showSuccessAnimation();
    
    this.userService.getCurrentUser().subscribe({
      next: (user: User) => {
        this.connectedUser = user;
        this.redirectUserBasedOnRole();
      },
      error: (err) => {
        console.error('Error fetching user data:', err);
        // Default redirect if user fetch fails
        this.router.navigate(['/tickets']);
      }
    });
  }

  
  private handleLoginError(err: any): void {
    this.isLoading = false;
    console.error('Login error:', err);
    
    if (err.error?.validationErrors) {
      this.errorMsg = err.error.validationErrors;
    } else if (err.error?.errorMsg) {
      this.errorMsg = [err.error.errorMsg];
    } else if (err.error?.message) {
      this.errorMsg = [err.error.message];
    } else {
      this.errorMsg = ['Une erreur est survenue lors de la connexion. Veuillez réessayer.'];
    }
    
    this.shakeForm();
  }

  
  private handleOAuthSuccess(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user: User) => {
        this.connectedUser = user;
        this.redirectUserBasedOnRole();
      },
      error: (err) => {
        this.toastr.error('Error fetching user data after OAuth', 'Error');
        console.error('Error fetching user data after OAuth:', err);
        this.router.navigate(['/tickets']);
      }
    });
  }

 
  private redirectUserBasedOnRole(): void {
    setTimeout(() => {
      if ( this.connectedUser?.role === 'MANAGER' || this.connectedUser?.role === 'ENGINEER') {
        this.router.navigate(['/getAllTicket']);
      } else if (this.connectedUser?.role === 'USER') {
        this.router.navigate(['/tickets']);
      }else if (this.connectedUser?.role === 'ADMIN') {
        this.router.navigate(['/user-management']);
      }
    }, 1000);
  }

  
  private showSuccessAnimation(): void {
    const cardElement = document.querySelector('.login-card');
    if (cardElement) {
      cardElement.classList.add('success-animation');
      setTimeout(() => {
        cardElement.classList.remove('success-animation');
      }, 600);
    }
  }

 
  private shakeForm(): void {
    const formElement = document.querySelector('.login-card');
    if (formElement) {
      formElement.classList.add('shake-animation');
      setTimeout(() => {
        formElement.classList.remove('shake-animation');
      }, 500);
    }
  }
}