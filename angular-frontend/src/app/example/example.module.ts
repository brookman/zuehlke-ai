import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExampleComponent } from './example-component/example.component';
import { HttpClientModule } from '@angular/common/http';
import { StyledButtonComponent } from '../shared/layout/styled-button/styled-button.component';
import { OtherComponent } from './other-component/other.component';
import { LoadingPageComponent } from '../shared/loading-page/loading-page.component';
import {ButtonModule} from "primeng/button";
import {ChatComponentComponent} from "./chat-component/chat-component.component";

@NgModule({
  declarations: [
    ExampleComponent,
    OtherComponent
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    StyledButtonComponent,
    LoadingPageComponent,
    ButtonModule,
    ChatComponentComponent
  ],
  exports: [
    ExampleComponent,
    OtherComponent
  ]
})
export class ExampleModule { }
