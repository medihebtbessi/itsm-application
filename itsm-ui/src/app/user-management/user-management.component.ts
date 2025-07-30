import { Component, OnInit } from '@angular/core';
import { SideBarAdminComponent } from "../side-bar-admin/side-bar-admin.component";
import {  User, UserService } from '../pages/user/user.service';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-user-management',
  imports: [NgFor,CommonModule,FormsModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {

  user: User=null!; ;
 
   ngOnInit(): void {
    this.getAllUsers();
  }
addNewUser() {
  this.router.navigate(['/register']);
}
onSearch() {
}
toggleSelectAll($event: Event) {
}
isAllSelected() {
}
toggleUserSelection(_t57: User) {
}
viewDetails(_t57: User) {
  this.userService.getUserByEmail(_t57.email).subscribe({
    next: (user) => {
      user = user;
    
      this.toastr.success('User details fetched successfully', 'Success');
      console.log('User details:', user);
    },
    error: (err) => {
      this.toastr.error('Error deleting user','Error')
      console.error('Error deleting user:', err);
    }});
}
deleteUser(_t57: User) {
  this.userService.deleteUser(_t57.email).subscribe({
    next: () => {
      console.log('User deleted successfully');
      this.toastr.success('User deleted successfully', 'Success');
      // Rafraîchir la liste des utilisateurs après la suppression
      this.getAllUsers();
    },
    error: (err) => {
      this.toastr.error('Error deleting user', 'Error');
    
      console.error('Error deleting user:', err);
    }
  });
}
editUser(_t57: User) {
  this.userService.updateUser(_t57.email, _t57).subscribe({
    next: (updatedUser) => {
      this.toastr.success('User updated successfully', 'Success');
      console.log('User updated successfully:', updatedUser);
      // Rafraîchir la liste des utilisateurs après la mise à jour
      this.getAllUsers();
    },
    error: (err) => {
      this.toastr.error('Error updating user', 'Error');
      console.error('Error deleting user:', err);
    }});
}

  constructor(private userService:UserService,private router:Router,private toastr: ToastrService) { }
  users: User[] = [];

 

  getAllUsers() {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.filteredUsers = [...this.users]; 
        this.toastr.success('','');
        console.log(users);
      },
      error: (err) => {
        this.toastr.error('','');
        console.error('Error fetching users:', err);
      }
    });
  }

   

  filteredUsers: User[] = [];
  searchTerm: string = '';
  activeTab: string = 'user';
  selectedPeriod: string = 'This Month';
  selectedSort: string = 'Alphabetic Order';

 

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  setPeriod(period: string): void {
    this.selectedPeriod = period;
    console.log('Period selected:', period);
  }

  setSort(sortType: string): void {
    this.selectedSort = sortType;
    this.sortUsers(sortType);
  }

  sortUsers(sortType: string): void {
    switch (sortType) {
      case 'Alphabetic Order':
        this.filteredUsers.sort((a, b) => a.firstname.localeCompare(b.firstname));
        break;
      case 'Date Order':
        this.filteredUsers.sort((a, b) => a.role.localeCompare(b.role));
        break;
      case 'Status Order':
        this.filteredUsers.sort((a, b) => a.role.localeCompare(b.role));
        break;
    }

}
}
