import { Injectable } from '@angular/core';
import { ComponentStore } from '@ngrx/component-store';
import { catchError, EMPTY, Observable, switchMap, tap, pipe } from 'rxjs';
import { Message } from '../models/message';
import { BackendService } from './backend.service';

export interface ChatState{
  sessionId:string|""
  chatRows:Message[]
  loader:boolean
}

export const getDefaultState:ChatState ={
  sessionId:"",
  chatRows:[],
  loader:false
}

@Injectable()
export class ChatStoreService extends ComponentStore<ChatState>{

  constructor(private backEndService:BackendService) { 

    const getCurrentState = localStorage.getItem("currentState")
    const initialState = getCurrentState ? JSON.parse(getCurrentState) : getDefaultState
    super(initialState)

    this.state$.subscribe(state => {
      localStorage.setItem("currentState",JSON.stringify(state))
    })
  }


  readonly sessionId$ = this.select(state => state.sessionId)
  readonly chatRows$ = this.select(state => state.chatRows)
  readonly loader$ = this.select(state => state.loader)


  readonly getMessage = this.effect((message$:Observable<any>) =>{
    return message$.pipe(
      switchMap(([message,sessionId]) => this.backEndService.chatWithBackEnd(message,sessionId).pipe(
        tap({
          next:(response:any) => {
            try{
              const rows = JSON.parse(response.fromAmadeus)
              const responseMethod = response.method
              console.log(rows)
              console.log(responseMethod)
              const newMessage:Message={"text":response.gptResponse,"sender":"Your Booking Companion",
                                        "timeStamp":new Date(), componentType:"_FlightOfferComponent", 
                                        componentData:rows}
              this.addChatRows(newMessage)  
            }catch(e){
              const newMessage:Message={"text":response.gptResponse,"sender":"Your Booking Companion",
                                        "timeStamp":new Date()}
              this.addChatRows(newMessage)
            }
            
          },
          error: (e) => console.log(e),
          complete:()=>this.setLoader(false)
        }),
        catchError(() => EMPTY),
      ))
    )
  })

  readonly completeOrder = this.effect((orders$:Observable<any>) => {
    return orders$.pipe(
      switchMap(([passengerDetails,flightOffer]) => this.backEndService.sendPassengerDetails(passengerDetails,flightOffer).pipe(
        tap({
          next:(response:any) => {
            const newMessage:Message={"text":response.gptResponse,"sender":"Your Booking Companion","timeStamp":new Date()}
            this.addChatRows(newMessage)
            },
            error:(e) => console.log(e),
            complete:()=>this.setLoader(false)
          }),
          catchError(() => EMPTY),
        ))
      )
    })




  readonly setLoader = this.updater((state,loader:boolean) => ({
    ...state,
    loader:loader
  }))

  readonly setSessionId = this.updater((state,sessionId:string)=>({
    ...state,
    sessionId:sessionId
  }))
  readonly setChatRows = this.updater((state, chatRows:Message[])=>({
    ...state,
    chatRows
  }))

  readonly addChatRows = this.updater((state,chatRow:Message) =>({
    ...state,
    chatRows: [...state.chatRows, chatRow]
  }))

  resetState = this.updater(() => getDefaultState)

}
