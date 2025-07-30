// profile-page.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NgClass, NgFor, NgIf } from '@angular/common';
import { UserService } from '../user/user.service';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.scss'],
  imports: [ NgClass, NgIf],
})
export class ProfilePageComponent implements OnInit {
  user: any;
  loading = true;
  error: string | null = null;

  constructor(
    private authService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.getUserProfile();
  }

  getUserProfile(): void {
    this.loading = true;
    this.authService.getCurrentUser().subscribe({
      next: (data) => {
        this.user = data;
        this.loading = false;
        console.log('User profile loaded:', this.user);
      },
      error: (err) => {
        console.error('Failed to load user profile:', err);
        this.error = 'Erreur lors du chargement du profil utilisateur';
        this.loading = false;
      }
    });
  }

  editProfile(): void {
    this.router.navigate(['/profile/edit']);
  }

  changePassword(): void {
    localStorage.clear();
    this.router.navigate(['/forget-password']);
  }

  refreshProfile(): void {
    this.getUserProfile();
  }
}