import { Component } from '@angular/core';
import { Group, RegistrationRequest, Role } from '../../models/RegistrationRequest';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-register',
  imports: [NgFor,FormsModule,NgIf],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {

  isLoading = false;
  showPassword = false;
  registerRequest : RegistrationRequest = {
    email: '',
    firstname : '',
    lastname : '',
    password : '',
    role : Role.ENGINEER ,
    group : Group.SOFTWARE
  };
  errorMsg: string[] = [];
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
    const passwordInput = document.getElementById('password') as HTMLInputElement;
    if (passwordInput) {
      passwordInput.type = this.showPassword ? 'text' : 'password';
    }
  }

  constructor(private router : Router ,
              private authService : AuthService,private toastr: ToastrService) {
  }

  groupOptions = [
  { value: 'SOFTWARE', label: 'Software', class: 'badge-new' },
  { value: 'NETWORK', label: 'Network', class: 'badge-in-progress' },
  { value: 'HARDWARE', label: 'Hardware', class: 'badge-on-hold' },
];

  register(){
    this.errorMsg=[];
    this.authService.register(
       this.registerRequest
    ).subscribe({
      next : () => {
        this.toastr.success('User added successfully','success')
        //this.router.navigate(['activation-account'])
      },
      error:(err)=>{
        this.toastr.error('Error to add user','Error')
        this.errorMsg = err.error.validationErrors;
      }
    });
  }

  login(){
    this.router.navigate(['login']);
  }
}
