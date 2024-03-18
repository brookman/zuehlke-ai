import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {InputTextareaModule} from "primeng/inputtextarea";
import {ButtonModule} from "primeng/button";
import {InputIconModule} from "primeng/inputicon";
import {IconFieldModule} from "primeng/iconfield";
import {InputGroupModule} from "primeng/inputgroup";
import {InputTextModule} from "primeng/inputtext";

@Component({
  selector: 'chat-component',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    InputTextareaModule,
    ButtonModule,
    InputIconModule,
    IconFieldModule,
    InputGroupModule,
    InputTextModule
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent implements OnInit{

  formGroup!: FormGroup;

  ngOnInit() {
    this.formGroup = new FormGroup({
      text: new FormControl<string | null>(null)
    });
  }

}
