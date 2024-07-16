import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'duration'
})
export class DurationPipe implements PipeTransform {

  transform(value:string): string {
    const pattern = /^PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$/;
    const matches = value.match(pattern);

    if (!matches) {
      return value;
    }

    const hours = matches[1] ? `${matches[1]} Hour${matches[1] === '1' ? '' : 's'}` : '';
    const minutes = matches[2] ? `${matches[2]} Minute${matches[2] === '1' ? '' : 's'}` : '';
    const seconds = matches[3] ? `${matches[3]} Second${matches[3] === '1' ? '' : 's'}` : '';

    return [hours, minutes, seconds].filter(Boolean).join(' ');
  }

  

}
