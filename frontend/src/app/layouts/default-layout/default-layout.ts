import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../../shared/components/sidebar/sidebar';
@Component({
  selector: 'app-default-layout',
  imports: [RouterOutlet,Sidebar],
  templateUrl: './default-layout.html',
  styleUrl: './default-layout.scss',
  standalone: true
})
export class DefaultLayout {

}
