import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { HTTP_INTERCEPTORS, provideHttpClient } from '@angular/common/http';
import { HttpTokenInterceptor } from './services/http-token.interceptor';
import { provideAnimations } from '@angular/platform-browser/animations';

import { provideToastr } from 'ngx-toastr';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpTokenInterceptor,
      multi: true
    },
    provideAnimations(), 
    //sprovideToastr(), 
    provideToastr({
  timeOut: 5000, 
  extendedTimeOut: 2000, 
  closeButton: true, 
  
  positionClass: 'toast-top-right', 
  
  preventDuplicates: true, 
  resetTimeoutOnDuplicate: true, 
  includeTitleDuplicates: true, 
  
  easeTime: 300, 
  enableHtml: false, 
  
  maxOpened: 5, 
  autoDismiss: true, 
  newestOnTop: true, 
  
  progressBar: true, 
  progressAnimation: 'increasing', 
  tapToDismiss: true, 
  
  toastClass: 'ngx-toastr', 
  titleClass: 'toast-title', 
  messageClass: 'toast-message', 
  iconClasses: {
    error: 'toast-error',
    info: 'toast-info',
    success: 'toast-success',
    warning: 'toast-warning'
  }
})
  ]
};
