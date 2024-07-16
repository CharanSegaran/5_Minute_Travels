import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ChatComponent } from './components/chat/chat.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { FlightOfferComponent } from './components/flight-offer/flight-offer.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DynamicComponentContainerDirective } from './components/dynamic-component-container.directive';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { DurationPipe } from './services/duration.pipe';
import { BookingsComponent } from './components/bookings/bookings.component';
import { ChatStoreService } from './services/chat-store.service';
import { JwtModule } from '@auth0/angular-jwt';
import { SignUpComponent } from './components/sign-up/sign-up.component';
import { AuthService } from './services/auth.service';
import { FaqComponent } from './components/faq/faq.component';

export function tokenGetter() {
  return localStorage.getItem("token");
}

@NgModule({
  declarations: [
    AppComponent,
    ChatComponent,
    FlightOfferComponent,
    DynamicComponentContainerDirective,
    DurationPipe,
    BookingsComponent,
    SignUpComponent,
    FaqComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule, ReactiveFormsModule,FormsModule,BrowserAnimationsModule, NgbModule,
    JwtModule.forRoot({
      config:{
        tokenGetter:tokenGetter
      }
    })
  ],
  providers: [ChatStoreService, AuthService],
  bootstrap: [AppComponent]
})
export class AppModule { }
