import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, FormsModule, Validators, ReactiveFormsModule } from '@angular/forms';
import { UserService, UserDto } from '../../services/user-service/user.service';
import { organisationDto, OrganisationService } from '../../services/organisation-service/organisation.service';
import { AuthService } from '../../services/auth-service';
import { TuiCheckbox } from '@taiga-ui/core';
import { GroupService } from '../../services/group-service/group.service';
import { Observable,forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-users-page',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TuiCheckbox],
  templateUrl: './admin-users-page.html',
  styleUrl: './admin-users-page.scss'
})
export class AdminUsersPage implements OnInit {
  users: UserDto[] = [];
  organisations: organisationDto[] = [];
  editingUid: string | null = null;
  editForm = { cn: '', sn: '', mail: '', o: '' };
  searchText = '';
  filteredUsers = [...this.users];
  userForm = new FormGroup({
    uid: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    sn: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    givenName: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    mail: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    o: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    telephoneNumber: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    ROLE_ADMINSGROUP: new FormControl<boolean>(false, { nonNullable: true }),
    ROLE_GESTIONNAIREUTILISATEURS: new FormControl<boolean>(false, { nonNullable: true }),
    ROLE_GESTIONNAIREORGANISATION: new FormControl<boolean>(false, { nonNullable: true })
  });
  message = '';
  showDetailsB: string | null = null;
  nextCookie: string | null = null;
  pageSize = 10;
  roles: String[] = [];

  constructor(
    private userService: UserService,
    private cdr: ChangeDetectorRef,
    private organisationService: OrganisationService,
    private authService: AuthService,
    private groupService: GroupService
  ) { }

  ngOnInit() {

    this.loadUsers();
    this.organisationService.listOrganisations().subscribe({
      next: organisations => {
        this.organisations = organisations;
        this.cdr.markForCheck();
      }
    })
    this.roles = this.authService.getRoles();
  }
  loadUsers(reset = true) {
    if (reset) {
      this.users = [];
      this.nextCookie = null;
    }

    this.userService.listUsersPaged(this.pageSize, this.nextCookie ?? undefined, this.searchText).subscribe({
      next: res => {
        this.users = reset ? res.items : [...this.users, ...res.items];
        this.nextCookie = res.nextPageCookie;
        this.filteredUsers = [...this.users];
        this.cdr.markForCheck();
      },
      error: err => {
        this.message = `Failed to load users: ${err.status}`;
        this.cdr.markForCheck();
      }
    });
  }
  search() {
    this.loadUsers();
  }
  loadMore() {
    this.loadUsers(false);
  }

  showDetails(user: UserDto) {
    this.showDetailsB = user.uid
  }
  // uid sn givenName mail mail o telephoneNumber
  startEdit(user: UserDto) {
    this.editingUid = user.uid;
    this.editForm = { cn: user.cn, sn: user.sn, mail: user.mail ?? '', o: user.o ?? '' };
    /* this.userForm.setValue({
      uid: user.uid,
      sn: user.sn,
      givenName: user.givenName,
      mail: user.mail,
      o: user.o,
      telephoneNumber: user.telephoneNumber,
      password: ''
    }); */
  }

  cancelEdit() {
    this.editingUid = null;
  }

  saveEdit(uid: string) {
    const { password, ROLE_ADMINSGROUP, ROLE_GESTIONNAIREORGANISATION, ROLE_GESTIONNAIREUTILISATEURS, ...userData } = this.userForm.getRawValue();
    this.userService.updateUser(uid, userData).subscribe({
      next: () => {
        this.message = 'User updated';
        this.editingUid = null;
        this.loadUsers();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.message = err.error?.error;
        // console.log(this.message);
        this.cdr.markForCheck();
      }
    });
  }

  resetPassword(uid: string) {
    const newPassword = prompt(`New password for ${uid}:`);
    if (!newPassword) return;

    this.userService.resetPassword(uid, newPassword).subscribe({
      next: () => this.message = `Password reset for ${uid}`,
      error: () => this.message = 'Failed to reset password'
    });
  }
  showCreateForm = false;
  userCount = "5";
  toggleCreateForm() {
    this.showCreateForm = !this.showCreateForm;
  }

  createUser() {
    if (this.userForm.invalid) {
      return;
    }
    const { ROLE_ADMINSGROUP, ROLE_GESTIONNAIREUTILISATEURS, ROLE_GESTIONNAIREORGANISATION, ...userData } = this.userForm.getRawValue();
    const rolesUser:Observable<any>[] = [];

    if (ROLE_ADMINSGROUP) {
      rolesUser.push(this.groupService.addMember("adminsGroup", userData.uid));
    }

    if (ROLE_GESTIONNAIREORGANISATION) {
      rolesUser.push(this.groupService.addMember("GestionnaireOrganisation", userData.uid));
    }

    if (ROLE_GESTIONNAIREUTILISATEURS) {
      rolesUser.push(this.groupService.addMember("GestionnaireUtilisateurs", userData.uid));
    }

    this.userService.createUser(userData).subscribe({
      next: () => {
        forkJoin(rolesUser).subscribe({
          next: () => {
            this.message = 'Utilisateur créé';
            this.userForm.reset();
            this.showCreateForm = false;
            this.loadUsers();
            this.userCount = (parseInt(this.userCount) + 1).toString();
            this.cdr.markForCheck();
          },
          error: err => {
            console.error("Failed adding roles", err);
          }
        });
      },
      error: err => {
        this.message = err.error?.error;
        this.cdr.markForCheck();
      }
    });
  }

  deleteUser(uid: string) {
    if (!confirm(`Delete user ${uid}? This cannot be undone.`)) return;

    this.userService.deleteUser(uid).subscribe({
      next: () => {
        this.message = `Utilisateur ${uid} supprimé`;
        this.loadUsers();
        this.cdr.markForCheck();
      },
      error: err => {
        this.message = `Failed to delete user: ${err.status}`;
        this.cdr.markForCheck();
      }
    });
  }
}