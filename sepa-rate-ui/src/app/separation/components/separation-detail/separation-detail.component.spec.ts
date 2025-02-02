import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SeparationDetailComponent } from './separation-detail.component';

describe('SeparationDetailComponent', () => {
  let component: SeparationDetailComponent;
  let fixture: ComponentFixture<SeparationDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SeparationDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SeparationDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
