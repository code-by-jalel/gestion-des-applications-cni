import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangePasswordPage } from './change-password-page';

describe('ChangePasswordPage', () => {
  let component: ChangePasswordPage;
  let fixture: ComponentFixture<ChangePasswordPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangePasswordPage],
    }).compileComponents();

    fixture = TestBed.createComponent(ChangePasswordPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
