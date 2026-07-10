import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminGroupsPage } from './admin-groups-page';

describe('AdminGroupsPage', () => {
  let component: AdminGroupsPage;
  let fixture: ComponentFixture<AdminGroupsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminGroupsPage],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminGroupsPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
