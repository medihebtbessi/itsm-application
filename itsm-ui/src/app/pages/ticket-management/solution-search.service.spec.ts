import { TestBed } from '@angular/core/testing';

import { SolutionSearchService } from './solution-search.service';

describe('SolutionSearchService', () => {
  let service: SolutionSearchService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SolutionSearchService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
