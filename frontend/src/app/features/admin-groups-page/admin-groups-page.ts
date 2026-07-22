import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TuiCheckbox } from '@taiga-ui/core';
import { GroupDto, GroupService } from '../../services/group-service/group.service';
import { organisationDto, OrganisationService } from '../../services/organisation-service/organisation.service';
import { UserDto, UserService } from '../../services/user-service/user.service';

interface GroupMemberRow {
  key: string;
  groupCn: string;
  memberUid: string;
  firstName: string;
  lastName: string;
  organisation: string;
}

function resolveFirstName(user: UserDto | undefined): string {
  return user?.prenom?.trim()
    || user?.firstName?.trim()
    || user?.sn?.trim()
    || user?.cn?.trim()
    || 'Non renseigné';
}

function resolveLastName(user: UserDto | undefined): string {
  return user?.nom?.trim()
    || user?.lastName?.trim()
    || user?.givenName?.trim()
    || 'Non renseigné';
}

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-admin-groups-page',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TuiCheckbox],
  templateUrl: './admin-groups-page.html',
  styleUrl: './admin-groups-page.scss'
})
export class AdminGroupsPage implements OnInit {
  groups: GroupDto[] = [];
  users: UserDto[] = [];
  memberRows: GroupMemberRow[] = [];
  message = '';
  messageType: 'success' | 'error' = 'success';
  searchText = '';

  showCreateForm = false;
  organisations: organisationDto[] = [];
  addMemberInput: Record<string, string> = {};
  roles = [
    'ROLE_ADMINSGROUP',
    'ROLE_GESTIONNAIREUTILISATEURS',
    'ROLE_GESTIONNAIREORGANISATION'
  ];

  newGroup = new FormGroup({
    cn: new FormControl<string>('', { nonNullable: true }),
    initialMemberUid: new FormControl<string>('', { nonNullable: true }),
    ROLE_ADMINSGROUP: new FormControl<boolean>(false, { nonNullable: true }),
    ROLE_GESTIONNAIREUTILISATEURS: new FormControl<boolean>(false, { nonNullable: true }),
    ROLE_GESTIONNAIREORGANISATION: new FormControl<boolean>(false, { nonNullable: true })
  });

  constructor(
    private groupService: GroupService,
    private userService: UserService,
    private cdr: ChangeDetectorRef,
    private organisationService: OrganisationService
  ) { }
    showAddMembersPanel = false;
    selectedUsers: Record<string, boolean> = {};
    targetGroupForAdd = '';
    memberSearch = '';

  ngOnInit() {
    this.loadGroups();
    this.loadUsers();

    this.organisationService.listOrganisations().subscribe({
      next: organisations => {
        this.organisations = organisations;
        this.cdr.markForCheck();
      }
    });
  }

  openAddMembersPanel() {
    this.showAddMembersPanel = true;
    if (this.users.length === 0) {
      this.userService.listUsers().subscribe({
        next: users => { this.users = users; this.cdr.markForCheck(); },
        error: err => this.notify(`Échec du chargement des utilisateurs: ${err.status}`, 'error')
      });
    }
  }

  toggleSelectUser(uid: string) {
    this.selectedUsers[uid] = !this.selectedUsers[uid];
    this.cdr.markForCheck();
  }

  toggleSelectAll(checked: boolean) {
    for (const u of this.users) this.selectedUsers[u.uid] = checked;
    this.cdr.markForCheck();
  }

  get filteredUsersForAdd(): UserDto[] {
    const term = this.memberSearch?.trim().toLowerCase();
    if (!term) return this.users;
    return this.users.filter(u =>
      (u.uid || '').toLowerCase().includes(term)
      || (u.mail || '').toLowerCase().includes(term)
      || (u.cn || '').toLowerCase().includes(term)
      || (u.prenom || '').toLowerCase().includes(term)
      || (u.sn || '').toLowerCase().includes(term)
      || (u.firstName || '').toLowerCase().includes(term)
      || (u.lastName || '').toLowerCase().includes(term)
    );
  }

  addSelectedUsersToGroup() {
    if (!this.targetGroupForAdd) { this.notify('Sélectionner un groupe cible', 'error'); return; }
    const uids = Object.keys(this.selectedUsers).filter(k => this.selectedUsers[k]);
    if (uids.length === 0) { this.notify('Sélectionner au moins un utilisateur', 'error'); return; }

    let remaining = uids.length;
    for (const uid of uids) {
      this.groupService.addMember(this.targetGroupForAdd, uid).subscribe({
        next: () => {
          this.notify(`${uid} ajouté à ${this.targetGroupForAdd}`, 'success');
          remaining--; if (remaining === 0) { this.showAddMembersPanel = false; this.selectedUsers = {}; this.loadGroups(); }
        },
        error: err => {
          this.notify(`Échec ajout ${uid}: ${err.status}`, 'error');
          remaining--; if (remaining === 0) { this.showAddMembersPanel = false; this.selectedUsers = {}; this.loadGroups(); }
        }
      });
    }
  }

  get filteredMemberRows(): GroupMemberRow[] {
    const term = this.searchText.trim().toLowerCase();
    if (!term) {
      return this.memberRows;
    }

    return this.memberRows.filter(row =>
      [row.groupCn, row.memberUid, row.firstName, row.lastName, row.organisation]
        .some(value => value.toLowerCase().includes(term))
    );
  }


  loadGroups() {
    this.groupService.listGroups().subscribe({
      next: groups => {
        this.groups = groups;
        this.rebuildMemberRows();
        this.cdr.markForCheck();
      },
      error: err => this.notify(`Échec du chargement: ${err.status}`, 'error')
    });
  }

  loadUsers() {
    this.userService.listUsers().subscribe({
      next: users => {
        this.users = users;
        this.rebuildMemberRows();
        this.cdr.markForCheck();
      },
      error: err => this.notify(`Échec du chargement des utilisateurs: ${err.status}`, 'error')
    });
  }

  private rebuildMemberRows() {
    const usersByUid = new Map(this.users.map(user => [user.uid, user]));

    this.memberRows = this.groups.flatMap(group =>
      group.members.map(uid => {
        const member = usersByUid.get(uid);

        return {
          key: `${group.cn}:${uid}`,
          groupCn: group.cn,
          memberUid: uid,
          firstName: resolveFirstName(member),
          lastName: resolveLastName(member),
          organisation: member?.o?.trim() || group.o?.trim() || 'Non renseignée'
        };
      })
    );
  }

  toggleCreateForm() {
    this.showCreateForm = !this.showCreateForm;
    if (!this.showCreateForm) {
      this.newGroup.reset();
    }
  }

  createGroup() {
    if (this.newGroup.invalid) return;

    const {
      cn,
      initialMemberUid,
      ROLE_ADMINSGROUP,
      ROLE_GESTIONNAIREUTILISATEURS,
      ROLE_GESTIONNAIREORGANISATION
    } = this.newGroup.getRawValue();

    this.groupService.createGroup(
      cn,
      initialMemberUid,
      ROLE_ADMINSGROUP,
      ROLE_GESTIONNAIREUTILISATEURS,
      ROLE_GESTIONNAIREORGANISATION
    ).subscribe({
      next: () => {
        this.notify('Groupe créé', 'success');
        this.newGroup.reset();
        this.showCreateForm = false;
        this.loadGroups();
        this.cdr.markForCheck();
      },
      error: err => this.notify(`Échec: ${err.error?.message ?? err.status}`, 'error')
    });
  }

  deleteGroup(cn: string) {
    if (!confirm(`Supprimer le groupe "${cn}" ? Cette action est irréversible.`)) return;

    this.groupService.deleteGroup(cn).subscribe({
      next: () => {
        this.notify(`Groupe ${cn} supprimé`, 'success');
        this.loadGroups();
      },
      error: err => this.notify(`Échec de la suppression: ${err.status}`, 'error')
    });
  }

  addMember(cn: string) {
    const uid = this.addMemberInput[cn]?.trim();
    if (!uid) return;

    this.groupService.addMember(cn, uid).subscribe({
      next: () => {
        this.notify(`${uid} ajouté à ${cn}`, 'success');
        this.addMemberInput[cn] = '';
        this.loadGroups();
      },
      error: err => this.notify(err.error?.error ?? 'Échec de l\'ajout', 'error')
    });
  }

  removeMember(cn: string, uid: string) {
    this.groupService.removeMember(cn, uid).subscribe({
      next: () => {
        this.notify(`${uid} retiré de ${cn}`, 'success');
        this.loadGroups();
      },
      error: () => this.notify('Impossible de retirer le dernier membre. Supprimez le groupe à la place.', 'error')
    });
  }

  private notify(msg: string, type: 'success' | 'error') {
    this.message = msg;
    this.messageType = type;
    this.cdr.markForCheck();
    setTimeout(() => {
      this.message = '';
      this.cdr.markForCheck();
    }, 4000);
  }
}
