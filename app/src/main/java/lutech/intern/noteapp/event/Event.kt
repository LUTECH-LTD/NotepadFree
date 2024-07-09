package lutech.intern.noteapp.event

sealed class Event {
    data object LoadNotesEvent : Event()

    data class SearchNotesEvent(val query: String?) : Event()

    data object SelectAllNotesEvent : Event()

    data object DeleteNotesEvent : Event()

    data object ClearSelectedNotesEvent : Event()

    data object ChangeCategoryNotesEvent : Event()
    data object ChangeColorNotesEvent : Event()
    data object ExportNotesEvent : Event()
    data object ImportNotesEvent : Event()
}