import { Component, ComponentRef, Type, ViewRef } from "@angular/core"


export interface Message{
    text: string
    sender: string
    timeStamp: Date
    componentType?:string
    componentData?:any
}
