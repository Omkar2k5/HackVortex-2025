import { redirect } from 'next/navigation'

// Server-side redirect to home page for better SEO and performance
export default function RootPage() {
  // This will be a server-side redirect, much faster than client-side
  redirect('/home')
}

