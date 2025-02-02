import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SeparationOverviewComponent } from './separation-overview.component';

describe('SeparationOverviewComponent', () => {
  let component: SeparationOverviewComponent;
  let fixture: ComponentFixture<SeparationOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SeparationOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SeparationOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
