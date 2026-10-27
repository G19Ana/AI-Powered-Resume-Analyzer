import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/resume';

  apiKey = signal<string>(localStorage.getItem('gemini_key') || '');
  jobTitle = signal<string>('Senior Java & Angular Developer');
  jobDescription = signal<string>('Looking for a Software Engineer proficient in Java 21, Spring Boot, PostgreSQL, Docker, AWS, microservices architecture, and Angular with reactive state management.');
  selectedFile: File | null = null;
  fileName = signal<string>('');

  loading = signal<boolean>(false);
  result = signal<any>(null);
  history = signal<any[]>([]);

  ngOnInit(): void {
    this.fetchHistory();
  }

  saveKey(k: string): void {
    this.apiKey.set(k);
    localStorage.setItem('gemini_key', k);
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (!file) return;
    this.selectedFile = file;
    this.fileName.set(file.name);
  }

  analyze(): void {
    if (!this.selectedFile || !this.jobDescription()) return;

    this.loading.set(true);
    const form = new FormData();
    form.append('file', this.selectedFile);
    form.append('jobTitle', this.jobTitle());
    form.append('jobDescription', this.jobDescription());

    let headers = new HttpHeaders();
    if (this.apiKey()) {
      headers = headers.set('X-Gemini-Key', this.apiKey());
    }

    this.http.post<any>(`${this.base}/analyze`, form, { headers }).subscribe({
      next: (res) => {
        this.result.set(res);
        this.loading.set(false);
        this.fetchHistory();
      },
      error: (err) => {
        alert(err?.error?.message || 'Analysis failed. Check your Gemini API key and PDF.');
        this.loading.set(false);
      }
    });
  }

  fetchHistory(): void {
    this.http.get<any[]>(`${this.base}/history`).subscribe(h => this.history.set(h));
  }

  loadHistorical(id: number): void {
    this.http.get<any>(`${this.base}/history/${id}`).subscribe(res => {
      this.result.set(res);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }

  deleteHistory(id: number, ev: Event): void {
    ev.stopPropagation();
    this.http.delete(`${this.base}/history/${id}`).subscribe(() => this.fetchHistory());
  }
}
