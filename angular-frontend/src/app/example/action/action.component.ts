import { Component } from '@angular/core';
import {NgClass} from "@angular/common";

@Component({
  selector: 'action',
  standalone: true,
  imports: [
    NgClass
  ],
  templateUrl: './action.component.html',
  styleUrl: './action.component.scss'
})
export class ActionComponent {

  hover: boolean = false;


  onMouseEnter() {
    this.hover = true;
  }

  onMouseLeave() {
    this.hover = false;
  }

}
