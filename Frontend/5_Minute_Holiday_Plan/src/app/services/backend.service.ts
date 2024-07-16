import { Injectable, TemplateRef } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Subject, firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BackendService {

  private messageSubject = new Subject<any>()
  message$ = this.messageSubject.asObservable()

  constructor(private http:HttpClient) { }

  chatWithBackEnd(message:string,sessionId:string){
    message = message.trim()
    const body = {message,sessionId}
    return this.http.post<any>("/api/NLP",body)
  }

  sendPassengerDetails(passengerDetails:any,flightOffer:any){
    const body = {passengerDetails,flightOffer}
    return this.http.post<any>("/api/createOrder",body)
  }
}
