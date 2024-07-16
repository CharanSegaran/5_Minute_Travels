import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ChatStoreService } from '../../services/chat-store.service';

@Component({
  selector: 'app-bookings',
  templateUrl: './bookings.component.html',
  styleUrl: './bookings.component.css'
})
export class BookingsComponent implements OnInit{
  
  rows:any[]=[]
  sessionId$ = this.chatStore.sessionId$

  constructor(private http:HttpClient, private chatStore:ChatStoreService){}

  ngOnInit(): void {
      this.http.get("/api/bookings").subscribe({
        next:(response:any)=>{
          this.rows = response.flightOrders
          console.log(this.rows)
        },
        error:(error:any)=>{
          console.log(error)
        }
      })
  }

  deleteBooking(id:string){
    this.http.delete(`/api/bookings/delete`, {params:{id:id}}).subscribe({
      next:(response:any)=>{
          this.rows = response.flightOrders
          console.log(this.rows)
          console.log(response)
      },
      error:(error:any)=>{
        console.log(error)
      }
    })
  }
}
