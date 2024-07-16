import { AfterViewChecked, AfterViewInit, Component, ComponentRef, ElementRef, Injector,OnDestroy,OnInit,QueryList,ViewChild,ViewChildren, ViewContainerRef} from '@angular/core';
import { Message } from '../../models/message';
import { FormGroup, FormBuilder, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';
import { BackendService } from '../../services/backend.service';
import { DynamicComponentContainerDirective } from '../dynamic-component-container.directive';
import { ChatStoreService } from '../../services/chat-store.service';
import { ActivatedRoute} from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css',
})
export class ChatComponent implements OnInit,AfterViewChecked{
  @ViewChildren(DynamicComponentContainerDirective) dynamicComponents!: QueryList<DynamicComponentContainerDirective>;
  @ViewChild('scrollMe') private messageContainer!: ElementRef;
  
  messageForm: FormGroup
  sessionId:string=""
  loading!:boolean
  shouldScroll: boolean = false;

  chatRows$ = this.chatStore.chatRows$
  sessionId$ = this.chatStore.sessionId$
  loading$ = this.chatStore.loader$


  constructor(private fb:FormBuilder, private backService:BackendService,
              private injector:Injector, private vcr:ViewContainerRef,
              private chatStore:ChatStoreService, private route:ActivatedRoute){
    this.messageForm = new FormGroup({
      text: new FormControl("",Validators.required)
    })
  }

  ngOnInit(): void {
      this.route.queryParams.subscribe(params => {
        this.sessionId = params["session"]
      })
      this.chatStore.setSessionId(this.sessionId)
      this.scrollToBottom();
    }

  ngAfterViewChecked(): void {
      if (this.shouldScroll) {
        this.scrollToBottom();
        this.shouldScroll = false; 
      }
    }
  
  sendToBackEnd(){
    const message = this.messageForm.get("text")?.value
    this.messageForm.reset()
    const newMessage:Message ={"text":message, "sender":"You", "timeStamp":new Date()}
    this.chatStore.addChatRows(newMessage)
    this.chatStore.getMessage([message, this.sessionId])
    this.chatStore.setLoader(true)
    this.shouldScroll = true
  }

  private scrollToBottom(): void {
    try {
      this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight;
    } catch (err) {
      console.error('Could not scroll to bottom', err);
    }
  }

}
