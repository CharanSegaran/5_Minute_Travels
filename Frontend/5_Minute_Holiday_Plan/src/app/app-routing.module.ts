import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChatComponent } from './components/chat/chat.component';
import { BookingsComponent } from './components/bookings/bookings.component';
import { yourGuardGuard } from './services/your-guard.guard';
import { SignUpComponent } from './components/sign-up/sign-up.component';
import { FaqComponent } from './components/faq/faq.component';

const routes: Routes = [
  {path:"", component:SignUpComponent},
  {path:"chat",component:ChatComponent, canActivate:[yourGuardGuard]},
  {path:"bookings",component:BookingsComponent,canActivate:[yourGuardGuard]},
  {path:"t&c",component:FaqComponent,canActivate:[yourGuardGuard]},
  //default to homepage
  {path:"**", redirectTo:"/", pathMatch:"full"}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
