import { CommonModule, NgFor } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  imports: [CommonModule,NgFor,RouterLink],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss'
})
export class FooterComponent {

   currentYear = new Date().getFullYear();
  
  socialLinks = [
    { name: 'Facebook', url: 'https://facebook.com', icon: 'fab fa-facebook-f' },
    { name: 'Twitter', url: 'https://twitter.com', icon: 'fab fa-twitter' },
    { name: 'LinkedIn', url: 'https://linkedin.com', icon: 'fab fa-linkedin-in' },
    { name: 'Instagram', url: 'https://instagram.com', icon: 'fab fa-instagram' }
  ];

  quickLinks = [
    { name: 'Accueil', url: '/' },
    { name: 'À propos', url: '/about' },
    { name: 'Services', url: '/services' },
    { name: 'Contact', url: '/contact' }
  ];

  legalLinks = [
    { name: 'Mentions légales', url: '/legal' },
    { name: 'Politique de confidentialité', url: '/privacy' },
    { name: 'Conditions d\'utilisation', url: '/terms' },
    { name: 'Cookies', url: '/cookies' }
  ];

  contactInfo = {
    email: 'contact@monsite.com',
    phone: '+33 1 23 45 67 89',
    address: '123 Rue de la Paix, 75001 Paris, France'
  };

  onSubscribe(email: string) {
    if (email) {
      console.log('Inscription newsletter:', email);
      // Ici vous pouvez ajouter la logique d'inscription
    }
  }
}
