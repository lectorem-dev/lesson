import ReactMarkdown from 'react-markdown'

type MarkdownLessonContentProps = {
  markdownContent: string
}

export function MarkdownLessonContent({ markdownContent }: MarkdownLessonContentProps) {
  if (!markdownContent.trim()) {
    return (
      <section className="lesson-markdown lesson-markdown--empty">
        <p>Материалы урока пока пустые.</p>
      </section>
    )
  }

  return (
    <section className="lesson-markdown">
      <ReactMarkdown>{markdownContent}</ReactMarkdown>
    </section>
  )
}
