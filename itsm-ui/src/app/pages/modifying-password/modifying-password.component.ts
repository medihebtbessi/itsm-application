import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-modifying-password',
  imports: [FormsModule,NgIf],
  templateUrl: './modifying-password.component.html',
  styleUrl: './modifying-password.component.scss'
})
export class ModifyingPasswordComponent {

  email: string = '';
  password: string = '';
  code: string = '';
  message: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  submitNewPassword() {
    this.authService.setNewPassword(this.email, this.password, this.code).subscribe({
      next: () => {
        this.message = 'Mot de passe modifié avec succès !';
        this.router.navigate(['/login']);
      },
      error: err => {
        console.error(err);
        this.message = 'Erreur lors de la modification du mot de passe.';
      }
    });
  }
}
