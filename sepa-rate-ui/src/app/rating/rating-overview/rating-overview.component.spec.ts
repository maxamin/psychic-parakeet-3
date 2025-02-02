import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RatingOverviewComponent } from './rating-overview.component';

describe('RatingOverviewComponent', () => {
  let component: RatingOverviewComponent;
  let fixture: ComponentFixture<RatingOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RatingOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RatingOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
