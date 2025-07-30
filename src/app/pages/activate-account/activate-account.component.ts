import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NgIf } from '@angular/common';
import { CodeInputComponent, CodeInputModule } from 'angular-code-input';
import { HttpClientModule } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-activate-account',
  imports: [NgIf, CodeInputModule],
  templateUrl: './activate-account.component.html',
  styleUrl: './activate-account.component.scss'
})
export class ActivateAccountComponent {
  message: string = '';
  isOkay: boolean = true;
  submitted: boolean = false;
  isLoading: boolean = false;

  constructor(
    private router: Router,
    private authService: AuthService,private toastr: ToastrService
  ) {}

  onCodeCompleted(token: string) {
    if (token && token.length === 6) {
      this.confirmAccount(token);
    }
  }

  redirectToLogin() {
    this.router.navigate(['login']);
  }

  resendCode() {
    // Implémenter la logique de renvoi du code
    console.log('Renvoi du code d\'activation');
    // this.authService.resendActivationCode().subscribe(...)
  }

  private confirmAccount(token: string) {
    this.isLoading = true;
    
    this.authService.activateAccount(token).subscribe({
      next: () => {
        this.message = 'Votre compte a été activé avec succès. Vous pouvez maintenant vous connecter.';
        this.toastr.success(this.message, 'Succès');
        this.submitted = true;
        this.isOkay = true;
        this.isLoading = false;
      },
      error: (error) => {
        this.message = 'Le code d\'activation a expiré ou est invalide. Veuillez vérifier et réessayer.';
        this.toastr.error(this.message, 'Erreur');
        this.submitted = true;
        this.isOkay = false;
        this.isLoading = false;
      }
    });
  }
}