import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { tokenGetter } from '../app.module';


@Injectable()
export class AuthService {

  constructor(public jwtHelper:JwtHelperService) {}

  public isAuthenticated():boolean{
    const token = tokenGetter()
    return !this.jwtHelper.isTokenExpired(token)
  }
}
