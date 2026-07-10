import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrganisationService, OrgNode } from '../../services/organisation-service/organisation.service';



@Component({
  selector: 'app-admin-structures-page',
  imports: [CommonModule, FormsModule, NgTemplateOutlet],
  templateUrl: './admin-structures-page.html',
  styleUrl: './admin-structures-page.scss',
})
export class AdminStructuresPage {
  tree: OrgNode[] = [];
  selectedNode: OrgNode | null = null;
  expandedDns = new Set<string>();

  message = '';
  messageType: 'success' | 'error' = 'success';

  showCreateForm = false;
  createParentDn = '';
  newOrg = { ou: '', description: '' };
  searchText='';
  editMode = false;
  editDescription = '';

  constructor(
    private orgService: OrganisationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadTree();
  }

  loadTree() {
    this.orgService.getTree(this.searchText).subscribe({
      next: tree => {
        console.log(tree);
        this.tree = tree;
        tree.forEach(n => this.expandedDns.add(n.dn));
        if (this.selectedNode) {
          this.selectedNode = this.findNode(tree, this.selectedNode.dn);
        }
        this.cdr.markForCheck();
      },
      error: err => this.showMessage(`Failed to load: ${err.status}`, 'error')
    });
  }
  search(){
    this.loadTree()
  }
  private findNode(nodes: OrgNode[], dn: string): OrgNode | null {
    for (const n of nodes) {
      if (n.dn === dn) return n;
      const found = this.findNode(n.children, dn);
      if (found) return found;
    }
    return null;
  }

  toggleExpand(dn: string, event: Event) {
    event.stopPropagation();
    this.expandedDns.has(dn) ? this.expandedDns.delete(dn) : this.expandedDns.add(dn);
    this.cdr.markForCheck();
  }

  selectNode(node: OrgNode) {
    this.selectedNode = node;
    this.editMode = false;
    this.showCreateForm = false;
    this.editDescription = node.description ?? '';
    this.cdr.markForCheck();
  }

  startAddChild(parentDn: string, event: Event) {
    event.stopPropagation();
    this.showCreateForm = true;
    this.createParentDn = parentDn;
    this.newOrg = { ou: '', description: '' };
    this.selectedNode = null;
    this.cdr.markForCheck();
  }

  createOrg() {
    if (!this.newOrg.ou.trim()) return;
    this.orgService.create(this.newOrg.ou, this.newOrg.description, this.createParentDn).subscribe({
      next: () => {
        this.showMessage('Organisation created', 'success');
        this.showCreateForm = false;
        this.expandedDns.add(this.createParentDn);
        this.loadTree();
      },
      error: err => this.showMessage(`Failed to create: ${err.error?.message ?? err.status}`, 'error')
    });
  }

  startEdit() {
    this.editMode = true;
    this.editDescription = this.selectedNode?.description ?? '';
    this.cdr.markForCheck();
  }

  saveEdit() {
    if (!this.selectedNode) return;
    this.orgService.update(this.selectedNode.dn, this.editDescription).subscribe({
      next: () => {
        this.showMessage('Organisation updated', 'success');
        this.editMode = false;
        this.loadTree();
      },
      error: err => this.showMessage(`Failed to update: ${err.status}`, 'error')
    });
  }

  cancelEdit() {
    this.editMode = false;
  }

  deleteNode() {
    if (!this.selectedNode) return;
    if (!confirm(`Delete "${this.selectedNode.ou}"? This cannot be undone.`)) return;

    this.orgService.delete(this.selectedNode.dn).subscribe({
      next: () => {
        this.showMessage('Organisation retiré', 'success');
        this.selectedNode = null;
        this.loadTree();
      },
      error: err => this.showMessage(err.error?.message ?? `Failed to delete: ${err.status}`, 'error')
    });
  }

  private showMessage(msg: string, type: 'success' | 'error') {
    this.message = msg;
    this.messageType = type;
    this.cdr.markForCheck();
    setTimeout(() => { this.message = ''; this.cdr.markForCheck(); }, 4000);
  }
}
