import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminStructuresPage } from './admin-structures-page';

describe('AdminStructuresPage', () => {
  let component: AdminStructuresPage;
  let fixture: ComponentFixture<AdminStructuresPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminStructuresPage],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminStructuresPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
