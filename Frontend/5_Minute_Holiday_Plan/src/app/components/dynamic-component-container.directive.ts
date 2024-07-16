import { ComponentRef, Directive, Input, OnInit, ViewContainerRef } from '@angular/core';
import { FlightOfferComponent } from './flight-offer/flight-offer.component';


const COMPONENT_MAPPING = {
  '_FlightOfferComponent': FlightOfferComponent
}

@Directive({
  selector: '[dynamic-component]'
})
export class DynamicComponentContainerDirective implements OnInit{
  @Input() componentType: string | undefined;
  @Input() componentData: any;

  ngOnInit() {
    this.createComponent();
  }

  private createComponent() {
    if (this.componentType) {
      const componentRef:ComponentRef<any> = this.vcr.createComponent( COMPONENT_MAPPING._FlightOfferComponent);
      componentRef.instance.data = this.componentData;
    }
  }

  constructor(private vcr: ViewContainerRef) {}

}
