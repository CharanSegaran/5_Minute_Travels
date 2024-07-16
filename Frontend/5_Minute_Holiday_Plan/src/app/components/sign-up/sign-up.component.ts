import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, OnInit, Renderer2 } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { NavigationEnd, Router } from '@angular/router';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { ChatStoreService } from '../../services/chat-store.service';
import { filter } from 'rxjs';

export const confirmPasswordValidator: ValidatorFn = (control:AbstractControl):ValidationErrors|null=>{
  const formGroup = control as FormGroup
  const password  = formGroup.get("signUpPassword")?.value
  const confirmPassword = formGroup.get("signUpConfirmPassword")?.value

  return password === confirmPassword? null : {PasswordsNoMatch:true}
}

export const StrongPasswordRegx: RegExp =
  /^(?=[^A-Z]*[A-Z])(?=[^a-z]*[a-z])(?=\D*\d).{8,}$/

export const EmailRegx: RegExp = /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$/

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent implements AfterViewInit,OnInit{

  //sign up abstract controls
  fullName:FormControl
  signUpEmail:FormControl
  signUpPassword:FormControl
  signUpConfirmPassword:FormControl
  signUpForm:FormGroup

  //log in abstract controls
  logInEmail:FormControl
  logInPassword:FormControl
  loginForm:FormGroup
  invalidCredentials:boolean = false


  constructor(private fb:FormBuilder, private http:HttpClient, private router:Router,
              private renderer:Renderer2, private chatStore:ChatStoreService){

    //sign up form                  
    this.signUpForm = this.fb.group({
      fullName: this.fullName = new FormControl("",[Validators.required]),
      signUpEmail: this.signUpEmail = new FormControl("",[Validators.required,
                                              Validators.pattern(EmailRegx)]),
      signUpPassword: this.signUpPassword = new FormControl("",[Validators.required,
                                                    Validators.pattern(StrongPasswordRegx)]),
      signUpConfirmPassword: this.signUpConfirmPassword = new FormControl("",[Validators.required])},
      {validators:confirmPasswordValidator})
    
      //log in form
      this.loginForm = this.fb.group({
        logInEmail:this.logInEmail = new FormControl("",[Validators.required]),
        logInPassword:this.logInPassword = new FormControl("",[Validators.required])
      })

      gsap.registerPlugin(ScrollTrigger)
  }

  ngOnInit(): void {
    this.chatStore.resetState()
    localStorage.clear()
    this.loadGsapAnimations()
    // this.router.events.pipe(filter(event => event instanceof NavigationEnd))
    //   .subscribe(() => {
    //     this.loadGsapAnimations()
    //   })
  }

  ngAfterViewInit(): void {
  }

  loadGsapAnimations(){

    gsap.registerPlugin(ScrollTrigger);

    this.renderer.listen("window","load", () => {
      const tl = 
      gsap
          .timeline({
          scrollTrigger: {
              trigger: ".wrapper",
              start: "top top",
              end: "+=50%",
              pin: true,
              scrub: true
          }
          })
          .to("img", {
          scale: 2,
          z: 450,
          transformOrigin: "centre left",
          ease: "power1.inOut"
          })
          .to(
          ".section.hero",
          {
              scale: 1.1,
              transformOrigin: "center center",
              ease: "power1.inOut"
          },
          "<"
          )
          tl.scrollTrigger?.refresh()
      })

      const container = document.getElementById('container');
      const overlayBtn = document.getElementById('overlayBtn');

      overlayBtn?.addEventListener('click', () => {
        container?.classList.toggle('right-panel-active');
      });
  }


  signUpSubmit(){
    if(this.signUpForm.invalid){
      this.signUpForm.markAllAsTouched()
      return;
    }else{
     
      this.http.post<any>("/api/auth/signup",this.signUpForm.value).subscribe({
        next:(response:any)=>{
          this.router.navigate(["/"])
          const container = document.getElementById('container');
          container?.classList.toggle('right-panel-active');
        },
        error:(error:any)=>{
          console.log(error)
        }
      })
   }
  }

  signInSubmit(){
    if(this.loginForm.invalid){
      this.loginForm.markAllAsTouched
      return;
    }else{
      this.http.post<any>("/api/auth/login",this.loginForm.value).subscribe({
        next:(response:any)=>{
          console.log(response)
          const id = crypto.randomUUID();
          localStorage.setItem("token",response.token)
          this.router.navigate(["/chat"], {queryParams:{session:id}})
        },
        error:(error:any)=>{
          console.log(error)
          this.invalidCredentials = true
        }
      })
    }
  }
}
