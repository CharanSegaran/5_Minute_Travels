import { AfterViewInit, Component, ElementRef, EventEmitter, Inject, Input, OnChanges, OnDestroy, OnInit, Output, Renderer2, SimpleChanges, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { BackendService } from '../../services/backend.service';
import { ChatStoreService } from '../../services/chat-store.service';

@Component({
  selector: 'app-flight-offer',
  templateUrl: './flight-offer.component.html',
  styleUrl: './flight-offer.component.css'
})
export class FlightOfferComponent implements OnChanges,OnInit{
  @Input() data:any
  @Input() method:string="GET"
  displayTable:boolean=true
  selectedFlightIndex: number | null = null;
  flightSelected!:string[]
  passengers:any[]=[]
  collapsedStates: boolean[] = [];

  allParticulars:FormArray[] =[]
  particulars:FormGroup

  displayForm:boolean=false
  displayMoreInfo:boolean=false
  private today = new Date()
  public maxDate = { year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate() }
  private passportMinDate = new Date(this.today.setMonth(this.today.getMonth() + 6));
  public minDatePassport = { year: this.passportMinDate.getFullYear(),
                            month: this.passportMinDate.getMonth() + 1, day: this.passportMinDate.getDate() }


  constructor(private fb:FormBuilder, private backService:BackendService,
              private chatStore:ChatStoreService){
    this.particulars = this.fb.group({
      passengers: this.fb.array([])
      })
    }

  ngOnChanges(changes: SimpleChanges): void {
      if(changes["method"]){
        this.displayTable = this.method === "GET"
        console.log(this.data)
    }
  }

  ngOnInit(): void {
    console.log(this.data)
    if(this.data){
      for(let i=0 ; i < this.data.flights[0].data.flightOffers[0].travelerPricings.length; i++){
        this.passengers.push(this.data.flights[0].data.flightOffers[0].travelerPricings[i]);
      }
      console.log("Passengers:",this.passengers)
      this.setPassengers(); 
    }
  
  }

  get passengersFormArray():FormArray{
    return this.particulars.get("passengers") as FormArray
  }

  setPassengers():void{
    this.passengers.forEach(passenger => {
      this.passengersFormArray.push(this.fb.group({
        passengerId: [passenger.travelerId, Validators.required],
        passengerType: [passenger.travelerType],
        dateOfBirth : ["",Validators.required],
        firstName : ["",Validators.required],
        lastName: ["",Validators.required],
        countryCallingCode: ["",Validators.required],
        mobileNumber: ["",Validators.required],
        passportNumber: ["",Validators.required],
        passportExpiryDate: ["",Validators.required],
        passportIssuanceCountry: ["",Validators.required],
        nationality: ["",Validators.required],
        email: ["",Validators.required]
      }))
    })
  }

  toggleAccordion(index: number): void {
    this.collapsedStates[index] = !this.collapsedStates[index];
  }

  submit(flightOffer:any){
    if(this.particulars.valid){
      const passengerDetails = this.particulars.value.passengers
      console.log("Passenger Details: ", passengerDetails)
      this.chatStore.completeOrder([passengerDetails,flightOffer])
      this.chatStore.setLoader(true)
    }else if(this.particulars.invalid){
      this.particulars.markAllAsTouched()
    }
  }

  selectFlight(flightOffer:any,i:number):void{
    this.displayForm = true
    this.selectedFlightIndex = i
  }

  moreInfoToggle(){
    this.displayMoreInfo = !this.displayMoreInfo
  }

}
